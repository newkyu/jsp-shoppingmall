package bookshop.bean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import work.crypt.SHA256;
import work.crypt.BCrypt;

public class LogonDBBean {
	
	//LogonDBBean ���� ��ü ���� <- �Ѱ��� ������ �����ؼ� ����
    private static LogonDBBean instance = new LogonDBBean();
    
    //LogonDBBean��ü�� �����ϴ� �޼ҵ�
    public static LogonDBBean getInstance() {
        return instance;
    }
    
    private LogonDBBean() {}
    
    //Ŀ�ؼ� Ǯ���� Ŀ�ؼ� ��ü�� ���� �޼ҵ�
    private Connection getConnection() throws Exception {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        DataSource ds = (DataSource)envCtx.lookup("jdbc/jsptest");
        return ds.getConnection();
    }
 
    //ȸ�� ���� ó������ ����ϴ� �޼ҵ�
    public void insertMember(LogonDataBean member){
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        SHA256 sha = SHA256.getInsatnce();
        try {
            conn = getConnection();
            
            String orgPass = member.getPasswd();
            String shaPass = sha.getSha256(orgPass.getBytes());
        	String bcPass = BCrypt.hashpw(shaPass, BCrypt.gensalt());
        	
            pstmt = conn.prepareStatement(
            	"insert into member values (?,?,?,?,?,?)");
            pstmt.setString(1, member.getId());
            pstmt.setString(2, bcPass);
            pstmt.setString(3, member.getName());
            pstmt.setTimestamp(4, member.getReg_date());
            pstmt.setString(5, member.getAddress());
            pstmt.setString(6, member.getTel());		
            pstmt.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
    }
 
	//�α��� �� ó���� ����� ���� ó������ ����ϴ� �޼ҵ�
    public int userCheck(String id, String passwd){
		Connection conn = null;
        PreparedStatement pstmt = null;
		ResultSet rs= null;
		int x=-1;
        
		SHA256 sha = SHA256.getInsatnce();
		try {
            conn = getConnection();
            
            String orgPass = passwd;
            String shaPass = sha.getSha256(orgPass.getBytes());
        	
            pstmt = conn.prepareStatement(
            	"select passwd from member where id = ?");
            pstmt.setString(1, id);
            rs= pstmt.executeQuery();

			if(rs.next()){//�ش� ���̵� ������ ����
				String dbpasswd= rs.getString("passwd"); 
				if(BCrypt.checkpw(shaPass,dbpasswd))
					x= 1; //��������
				else
					x= 0; //��й�ȣ Ʋ��
			}else//�ش� ���̵� ������ ����
				x= -1;//���̵� ����
			
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
			if (rs != null) try { rs.close(); } catch(SQLException ex) {}
            if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
		return x;
	}
    
    //���̵� �ߺ� Ȯ�ο��� ���̵��� �ߺ� ���θ� Ȯ���ϴ� �޼ҵ�
	public int confirmId(String id) {
		Connection conn = null;
        PreparedStatement pstmt = null;
		ResultSet rs= null;
		int x=-1;
        
		try {
            conn = getConnection();
            
            pstmt = conn.prepareStatement(
            	"select id from member where id = ?");
            pstmt.setString(1, id);
            rs= pstmt.executeQuery();

			if(rs.next())//���̵� ����
				x= 1; //���� ���̵� ����
			else
				x= -1;//���� ���̵� ����
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
			if (rs != null) try { rs.close(); } catch(SQLException ex) {}
            if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
		return x;
	}

    //�־��� id�� �ش��ϴ� ȸ�������� ���� �޼ҵ�
	public LogonDataBean getMember(String id){
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    LogonDataBean member=null;
	        
	    try {
	        conn = getConnection();
	            
	        pstmt = conn.prepareStatement(
	        	"select * from member where id = ?");
	        pstmt.setString(1, id);
	        rs = pstmt.executeQuery();
	     
	       if (rs.next()) {//�ش� ���̵� ���� ���ڵ尡 ����
	            	member = new LogonDataBean();//����������� ��ü����
	                member.setId(rs.getString("id"));
					member.setName(rs.getString("name"));
	                member.setReg_date(rs.getTimestamp("reg_date"));
	                member.setAddress(rs.getString("address"));
	                member.setTel(rs.getString("tel"));
	       }
	    } catch(Exception ex) {
	        ex.printStackTrace();
	    } finally {
	        if (rs != null) try { rs.close(); } catch(SQLException ex) {}
	        if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
	        if (conn != null) try { conn.close(); } catch(SQLException ex) {}
	    }
		return member;//������ ����� ��ü member ����
	}
	
	//�־��� id, passwd�� �ش��ϴ� ȸ�������� ���� �޼ҵ�
    public LogonDataBean getMember(String id, String passwd){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        LogonDataBean member=null;
        
        SHA256 sha = SHA256.getInsatnce();
        try {
            conn = getConnection();
            
            String orgPass = passwd;
            String shaPass = sha.getSha256(orgPass.getBytes());
            
            pstmt = conn.prepareStatement(
            	"select * from member where id = ?");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
     
            if (rs.next()) {//�ش� ���̵� ���� ���ڵ尡 ����
            	String dbpasswd= rs.getString("passwd");
            	//����ڰ� �Է��� ��й�ȣ�� ���̺��� ��й�ȣ�� ������ ����
				if(BCrypt.checkpw(shaPass,dbpasswd)){
                  member = new LogonDataBean();//����������� ��ü����
                  member.setId(rs.getString("id"));
				  member.setName(rs.getString("name"));
                  member.setReg_date(rs.getTimestamp("reg_date"));
                  member.setAddress(rs.getString("address"));
                  member.setTel(rs.getString("tel"));
				}
			}
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch(SQLException ex) {}
            if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
		return member;//������ ����� ��ü member ����
    }
    
    //ȸ�� ���� ������ ó���ϴ� �޼ҵ�
    @SuppressWarnings("resource")
	public int updateMember(LogonDataBean member){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs= null;
        int x=-1;
        
        SHA256 sha = SHA256.getInsatnce();
        try {
            conn = getConnection();
            
            String orgPass = member.getPasswd();
            String shaPass = sha.getSha256(orgPass.getBytes());
            
            pstmt = conn.prepareStatement(
                	"select passwd from member where id = ?");
            pstmt.setString(1, member.getId());
            rs = pstmt.executeQuery();
                
            if(rs.next()){//�ش� ���̵� ������ ����
				String dbpasswd= rs.getString("passwd"); 
				if(BCrypt.checkpw(shaPass,dbpasswd)){
                    pstmt = conn.prepareStatement(
                     "update member set name=?,address=?,tel=? "+
                     "where id=?");
                    pstmt.setString(1, member.getName());
                    pstmt.setString(2, member.getAddress());
                    pstmt.setString(3, member.getTel());
                    pstmt.setString(4, member.getId());
                    pstmt.executeUpdate();
                    x= 1;//ȸ������ ���� ó�� ����
				}else
					x= 0;//ȸ������ ���� ó�� ����
			}
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
        	if (rs != null) try { rs.close(); } catch(SQLException ex) {}
            if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
        return x;
    }
    
    //ȸ�� ������ �����ϴ� �޼ҵ�
    @SuppressWarnings("resource")
	public int deleteMember(String id, String passwd){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs= null;
        int x=-1;
        
        SHA256 sha = SHA256.getInsatnce();
        try {
			conn = getConnection();
			
			String orgPass = passwd;
            String shaPass = sha.getSha256(orgPass.getBytes());

            pstmt = conn.prepareStatement(
            	"select passwd from member where id = ?");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            
			if(rs.next()){
				String dbpasswd= rs.getString("passwd"); 
				if(BCrypt.checkpw(shaPass,dbpasswd)){
					pstmt = conn.prepareStatement(
            	      "delete from member where id=?");
                    pstmt.setString(1, id);
                    pstmt.executeUpdate();
					x= 1;//ȸ��Ż��ó�� ����
				}else
					x= 0;//ȸ��Ż�� ó�� ����
			}
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch(SQLException ex) {}
            if (pstmt != null) try { pstmt.close(); } catch(SQLException ex) {}
            if (conn != null) try { conn.close(); } catch(SQLException ex) {}
        }
		return x;
    }
}