import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.directory.*;

public class LdapTest {
	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		String ntUserId = "자신이 사용 할 ADMIN ID";//(예: cn=Administrator,cn=admin,dc=admin,dc=com)
		String ntPasswd = "자신이 사용 할 ADMIN PWD";
		String url = "LDAP 서버에서 사용 하고 있는 IP";//(예: LDAP://127.0.0.0)
		
		
		try{
			String usrId   = "Admin 계정에서 등록한 아이디";//------------------------------------(예: test001)
			String usrPw   = "Admin 계정에서 등록한 아이디의 패스워드";//----------------------(예: test001!)
			String baseRdn = "ou=admin,ou=administrator,dc=admin,dc=com";//----------------예시입니다.
			
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, url);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, ntUserId);
			env.put(Context.SECURITY_CREDENTIALS, ntPasswd);
			LdapContext ctx = new InitialLdapContext(env, null);

			System.out.println("Active Directory Connection: Connected");
			
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			String searchFilter = String.format("(&(objectClass=person)(sAMAccountName=%s))", usrId);
			NamingEnumeration<SearchResult> results = ctx.search(baseRdn, searchFilter, ctls);
			
			if (results.hasMore()) {
				Attributes attrs = ((SearchResult) results.next()).getAttributes();
				NamingEnumeration<String> ne = attrs.getIDs();
				while(ne.hasMore()){
					String id = ne.next();
					System.out.println("id:" + id + ", attr:" + attrs.get(id));
				}
			}else{
				throw new AuthenticationException("No Such Object");
			}
			
			Hashtable<String, String> usrEnv = new Hashtable<String, String>();
			usrEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			usrEnv.put(Context.PROVIDER_URL, url);
			usrEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
			usrEnv.put(Context.SECURITY_PRINCIPAL, usrId);
			usrEnv.put(Context.SECURITY_CREDENTIALS, usrPw);
			new InitialLdapContext(usrEnv, null);
			
			System.out.println("성공!!!!");
			
		}catch(AuthenticationException e){
			String msg = e.getMessage();
			
			 if (msg.indexOf("No Such Object") > -1){              //LDAP: error code 32 - No Such Object
					System.out.println("입력한 ID가 LDAP 상에 존재하지 않습니다. 확인 후 다시 시도해 주십시오.");
			} else if (msg.indexOf("error code 49") > -1) {
				if ((msg.indexOf("password expired") > -1) || (msg.indexOf("data 532") > -1)){     //LDAP: error code 49 - password expired!
					System.out.println("입력한 ID는 비밀번호의 유효기간이 만료되었습니다. 비밀번호를 재설정한 후에 다시 시도해 주십시오.");
				} else if ((msg.indexOf("Invalid Credentials") > -1) || (msg.indexOf("data 52e") > -1)){  //LDAP: error code 49 - Invalid Credentials
					System.out.println("ID와 비밀번호가 일치하지 않습니다.확인 후 다시 시도해 주십시오.");
				} else if (msg.indexOf("data 533") > 0){
					System.out.println("입력한 ID는 비활성화 상태 입니다. LDAP인증 서버 관리자에게 문의해 주십시오.");
				} else if(msg.indexOf("data 532") > 0){
					System.out.println("AD에서 로그온 거부를 체크하였습니다.");
				} else if(msg.indexOf("data 701") > 0){
					System.out.println("AD에서 계정이 만료됨");
				} else {
					System.out.println("정상!");
				}
			}else {
				System.out.println("인증 전단계에서 사용자가 있는지만 체크");
			}
		}catch(Exception nex){
			String msg2 = nex.getMessage();
			
			System.out.println("LDAP Connection: FAILED");
			nex.printStackTrace();
			if(msg2.indexOf("Account inactivated") > -1){
				System.out.println("입력한 ID는 비활성화 상태 입니다. LDAP인증 서버 관리자에게 문의해 주십시오.");
			}
		}
	}

}