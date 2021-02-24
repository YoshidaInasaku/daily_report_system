package models.validators;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import models.Employee;
import utils.DBUtil;

public class EmployeeValidator {
    // 各カラムのバリデーションを実行
    public static List<String> validate(Employee e, Boolean codeDuplicateCheckFlag, Boolean passwordCheckFlag) {
        List<String> errors = new ArrayList<String>();  // エラー内容を格納するリストを生成


        // 社員番号
        String code_error = validateCode(e.getCode(), codeDuplicateCheckFlag);  // 社員番号のバリデーションを実行
        if (!code_error.equals("")) {
            errors.add(code_error);
        }

        // 社員氏名
        String name_error = validateName(e.getName());  // 社員氏名のバリデーションを実行
        if (!name_error.equals("")) {
            errors.add(name_error);
        }

        // パスワード
        String password_error = validatePassWord(e.getPassword(), passwordCheckFlag);  // パスワードのバリデーションを実行
        if (!password_error.equals("")) {
            errors.add(password_error);
        }

        return errors;
    }



    // 社員番号のバリデーションを実行
    private static String validateCode(String code, Boolean codeDuplicateCheckFlag) {
        // 必須入力チェック
        if (code == null || code.equals("")) {
            return "社員番号を入力してください";
        }

        // すでに登録されている社員番号との重複チェック
        if (codeDuplicateCheckFlag) {
            EntityManager em = DBUtil.createEntityManager();
            long employees_count = (long)em.createNamedQuery("checkRegisterdCode", Long.class).setParameter("code", code).getSingleResult();  // queryを実行
            em.close();

            // DBにすでに登録ずみであればバリデーションを実行
            if (employees_count > 0) {
                return "入力された社員番号の情報はすでに存在しています";
            }
        }

        // 何もなければそのままリターン
        return "";
    }


    // 社員氏名のバリデーションを実行
    private static String validateName(String name) {
        // 必須入力チェック
        if (name == null || name.equals("")) {
            return "氏名を入力してください";
        }

        return "";
    }


    // パスワードのバリデーションを実行
    private static String validatePassWord(String password, Boolean passwordCheckFlag) {
        // パスワードを変更する際にのみバリデーションを実行
        if (passwordCheckFlag && (password == null || password.equals(""))) {
            return "パスワードを入力してください";
        }

        return "";
    }



}
