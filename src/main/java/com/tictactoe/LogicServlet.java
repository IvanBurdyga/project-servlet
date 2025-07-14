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
        HttpSession currentSession = req.getSession();
        Field field = extractField(currentSession);
        if (checkWin(currentSession, resp, field)) {
            return;
        }
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);
        if (currentSign != Sign.EMPTY) {
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.jsp");
            rd.forward(req, resp);
            return;
        }
        field.getField().put(index, Sign.CROSS);
        if (checkWin(currentSession, resp, field)) {
            return;
        }
        int emptyFieldIndex = field.getEmptyFieldIndex();
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(currentSession, resp, field)) {
                return;
            }
        } else {
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            resp.sendRedirect("/index.jsp");
            return;
        }
        List<Sign> data = field.getFieldData();
        currentSession.setAttribute("field", field);
        currentSession.setAttribute("data", data);
        resp.sendRedirect("/index.jsp");
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private int getSelectedIndex(HttpServletRequest req) {
        String click = req.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private boolean checkWin(HttpSession currentSession, HttpServletResponse resp, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (winner == Sign.CROSS || winner == Sign.NOUGHT) {
            currentSession.setAttribute("winner", winner);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            resp.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
}
