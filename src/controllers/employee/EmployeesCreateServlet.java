package controllers.employee;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import models.validators.EmployeeValidator;
import utils.DBUtil;
import utils.EncryptUtil;

/**
 * Servlet implementation class EmployeesCreateServlet
 */
@WebServlet("/employees/create")
public class EmployeesCreateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmployeesCreateServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String _token = request.getParameter("_token");

        if (_token != null && _token.equals(request.getSession().getId())) {  // CSRF対策
            EntityManager em = DBUtil.createEntityManager();

            Employee e = new Employee();

            e.setCode(request.getParameter("code"));
            e.setName(request.getParameter("name"));
            e.setPassword(
                    EncryptUtil.getPasswordEncrypt(
                            request.getParameter("password"),
                            (String)this.getServletContext().getAttribute("pepper")  // アプリケーションスコープにpepper文字列を登録
                            )
                    );
            e.setAdmin_flag(Integer.parseInt(request.getParameter("admin_flag")));

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            e.setCreated_at(currentTime);
            e.setUpdated_at(currentTime);
            e.setDelete_flag(0);  // 作成のサーブレットなので削除フラグは0

            // 新規登録なので,codeDuplicateCheck/passwordCheckを共に行いたいためtrueでセット
            List<String> errors = EmployeeValidator.validate(e, true, true);
            if (errors.size() > 0) {
                em.close();  // DB終了

                request.setAttribute("_token", request.getSession().getId());
                request.setAttribute("employee", e);
                request.setAttribute("errors", errors);

                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/new.jsp");
                rd.forward(request, response);
            } else {
                em.getTransaction().begin();
                em.persist(e);
                em.getTransaction().commit();
                request.getSession().setAttribute("flush", "登録が完了しました。");
                em.close();

                response.sendRedirect(request.getContextPath() + "/employees/index");
            }
        }
    }

}
