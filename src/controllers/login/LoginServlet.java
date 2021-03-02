package controllers.login;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import utils.DBUtil;
import utils.EncryptUtil;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    // ログイン画面を表示
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("_token", request.getSession().getId());
        request.setAttribute("hasError", false);

        if(request.getSession().getAttribute("flush") != null) {
            request.setAttribute("flush", request.getSession().getAttribute("flush"));
            request.getSession().removeAttribute("flush");
        }

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/login/login.jsp");
        rd.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    // ログイン処理を実行
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 認証結果を格納する変数
        Boolean check_result = false;  // ログインチェックの結果の初期値

        String code = request.getParameter("code");
        String plain_pass = request.getParameter("password");

        Employee e = null;  // 社員の初期値

        // 社員番号の入力とパスワードの入力ができていたら処理
        if(code != null && !code.equals("") && plain_pass != null && !plain_pass.equals("")) {
            EntityManager em = DBUtil.createEntityManager();

            String password = EncryptUtil.getPasswordEncrypt(
                    plain_pass,
                    (String)this.getServletContext().getAttribute("pepper")
                    );

            // 入力されたものがDBに登録されている社員番号とパスワードと照合するかチェック
            try {
                e = em.createNamedQuery("checkLoginCodeAndPassword", Employee.class)
                        .setParameter("code", code)
                        .setParameter("pass", password)
                        .getSingleResult();
            } catch (NoResultException ex) {}

            em.close();

            if(e != null) {
                check_result = true;
            }
        }

        // 認証処理がなされなかったらログイン画面に戻る（パスワードが間違っていたためDB照合に失敗)
        if(!check_result) {
            request.setAttribute("_token", request.getSession().getId());
            request.setAttribute("hasError", true);
            request.setAttribute("code", code);

            RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/login/login.jsp");
            rd.forward(request, response);
        } else {
            // 認証処理できたら認証状態にして（check_resultをtrue)ログイン画面に戻る
            request.getSession().setAttribute("login_employee", e);  // Sessionにログインユーザーを保存

            request.getSession().setAttribute("flush", "ログインしました。");
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

}
