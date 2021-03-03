package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import models.Employee;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter("/*")
public class LoginFilter implements Filter {

    /**
     * Default constructor.
     */
    public LoginFilter() {
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String context_path = ((HttpServletRequest)request).getContextPath();  // ServletRequest型をHttpServletRequest型にキャスト
        String servlet_path = ((HttpServletRequest)request).getServletPath();

        if(!servlet_path.matches("/css.*")) {  // CSSフォルダ内は認証処理から除外する
            HttpSession session = ((HttpServletRequest)request).getSession();  // sessionを開始

            // セッションスコープに保存された従業員（ログインユーザ）情報を取得
            Employee e = (Employee)session.getAttribute("login_employee");

            // ログイン画面以外について
            if(!servlet_path.equals("/login")) {
                // ログアウトしている状態であれば、ログイン画面にリダイレクト
                if(e == null) {
                    ((HttpServletResponse)response).sendRedirect(context_path + "/login");
                    return;
                }

                // 従業員管理の機能は管理者のみが閲覧できるようにする
                if(servlet_path.matches("/employees.*") && e.getAdmin_flag() == 0) {  // 一般従業員はトップページにリダイレクトさせる
                    ((HttpServletResponse)response).sendRedirect(context_path + "/");
                    return;
                }

            // ログイン画面について
            } else {
                // ログインしているのにログイン画面を表示させようとした場合はシステムのトップページにリダイレクト
                if(e != null) {
                    ((HttpServletResponse)response).sendRedirect(context_path + "/");
                    return;
                }
            }

        }

        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
    }

}
