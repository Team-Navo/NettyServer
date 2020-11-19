package Database;


import java.sql.*;

public class DatabaseConnection {
    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"; // 드라이버
    private final String DB_URL = "jdbc:mysql://localhost:3306/user?&serverTimezone=UTC&useSSL=false";  // 접속할 DB 서버
    private final static String USER_NAME = "root"; // DB에 접속할 사용자 이름
    private final static String PASSWORD = "cpb12345"; // 사용자 비밀번호
    private static DatabaseConnection connector;
    private static Connection conn;

    public DatabaseConnection(){
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getConnector() {
        if(connector == null)
            connector = new DatabaseConnection();
        return connector;
    }

    // ID 중복 체크
    public boolean idCheck(User user) {
        String sql = "SELECT id FROM user WHERE id=?;";
        PreparedStatement pstate = null;
        boolean result = false;
        try {
            pstate = conn.prepareStatement(sql);
            pstate.setString(1, user.getId());
            ResultSet rs = pstate.executeQuery();

            rs.next();
            result = rs.getString(1).equals(user.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstate != null && !pstate.isClosed())
                    pstate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 회원가입
    public boolean createUser(User user) {
        String sql = "insert into user values(?, ?, ?, ?, ?);";
        PreparedStatement pstate = null;
        boolean result = false;
        try {
            if(!idCheck(user)) { // id 중복 체크
                pstate = conn.prepareStatement(sql);
                pstate.setString(1, user.getId());
                pstate.setString(2, user.getPw());
                pstate.setString(3, user.getName());
                pstate.setString(4, user.getBirth());
                pstate.setString(5, user.getPhone());
                int rs = pstate.executeUpdate();

                result = rs == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstate != null && !pstate.isClosed())
                    pstate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // id 찾기
    public String findID(User user) {
        String sql = "SELECT id FROM user WHERE name=? AND birth=?;";
        PreparedStatement pstate = null;

        try {
            pstate = conn.prepareStatement(sql);
            pstate.setString(1, user.getName());
            pstate.setString(2, user.getBirth());
            ResultSet rs = pstate.executeQuery();

            if(rs.next())
                return rs.getString("id");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstate != null && !pstate.isClosed())
                    pstate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // pw 찾기
    public String findPW(User user) {
        String sql = "SELECT pw FROM user WHERE id=? AND name=?;";
        PreparedStatement pstate = null;

        try {
            pstate = conn.prepareStatement(sql);
            pstate.setString(1, user.getId());
            pstate.setString(2, user.getName());
            ResultSet rs = pstate.executeQuery();

            if(rs.next())
                return rs.getString("pw");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstate != null && !pstate.isClosed())
                    pstate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    //로그인
    public boolean userLogin(User user) {
        String sql = "SELECT pw FROM user WHERE id = ?;";
        PreparedStatement pstate = null;
        boolean result = false;

        try {
            pstate = conn.prepareStatement(sql);
            pstate.setString(1, user.getId());
            ResultSet rs = pstate.executeQuery();
            if(rs.next()) {
                if(rs.getString(1).equals(user.getPw()))
                    result = true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstate != null && !pstate.isClosed())
                    pstate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
