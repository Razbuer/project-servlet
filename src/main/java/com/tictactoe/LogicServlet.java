package com.tictactoe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Получаем текущую сессию
        HttpSession currentSession = req.getSession();

        // Получаем объект игрового поля из сессии
        Field field = extractFiled(currentSession);

        // Поулчаем индекс ячейки, по которой произошёл клик
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        // Проверяем, что ячейка, по которой был клик пустая.
        // Иначе ничего не делаем и отправляем пользователя на ту же страницу без изменений
        // параметров в сессии
        if (currentSign != Sign.EMPTY) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // Ставим крестик в ячейке, по которой кликнул пользователь
        field.getField().put(index, Sign.CROSS);

        // Проверяем, не победил ли крестик
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // Получаем первую пустую ячейку поля
        int emptyFieldIndex = field.getEmptyFieldIndex();

        // Проверяем что есть пустые ячейки, иначе ничья
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            // Проверяем не победил ли нолики
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            // Добавляем в сессию фдаг, который сигнализирует что произошла ничья
            currentSession.setAttribute("draw", true);

            // Считываем список значков
            List<Sign> data = field.getFieldData();

            // Обновляем список значков
            currentSession.setAttribute("data", data);

            // Редиректим
            resp.sendRedirect("/index.jsp");
            return;
        }

        // Считаем список значков
        List<Sign> data = field.getFieldData();

        // Обновляем объект поля и список значков в сессии
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");
    }

    // Получаем атрибут filed из сессии
    private Field extractFiled(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");

        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }

        return (Field)fieldAttribute;
    }

    // Получаем номер ячейки по которой кликнули
    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    // Проверяем нет ли трех крестиков/ноликов в ряд.
    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            // Добавляем флаг, который показывает что кто-то победил
            currentSession.setAttribute("winner", winner);

            // Считываем  список значков
            List<Sign> data = field.getFieldData();
            // Обновляем этот список в сессии
            currentSession.setAttribute("data", data);

            // Редиректим
            response.sendRedirect("/index.jsp");

            return true;
        }

        return false;
    }


}
