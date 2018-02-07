[JAVA] Active Directory(LDAP) 연결 예제 소스





Active Directory와 JAVA를 연결 할 때 사용되는 소스입니다.

구글링에서 한 결과를 바탕으로 저만의 방식으로 재구현 해보았습니다.



Active Directory는 LDAP 연결과 같은 방식으로 연결되기 때문에 LDAP 연결에서도 사용하실 수 있습니다.





public class ADTest {



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



 System.out.println("Active Directory Connection: CONNECTED");



 // Hashtable 부터 LdapContext까지 LDAP 접속의 대한 인증을 합니다. ntUserId, ntPasswd, url 세가지로 연결 확인을 합니다.

 // 정상적인 연결이 되면 "CONNECTED"가 출력됩니다.



			SearchControls ctls = new SearchControls();

			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			ctls.setReturningAttributes(new String[] {"cn"});



       // 인증이 확인 됬다면 usrId, usrPw, baseRdn(유저가 등록된 위치)으로 Admin에서 등록한 유저를 찾아봅시다!

			

 String searchFilter = String.format("(cn=%s)", usrId);

			NamingEnumeration results = ctx.search(baseRdn, searchFilter, ctls);



			Hashtable<String, String> usrEnv = new Hashtable<String, String>();

			usrEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

			usrEnv.put(Context.PROVIDER_URL, url);

			usrEnv.put(Context.SECURITY_AUTHENTICATION, "simple");

			usrEnv.put(Context.SECURITY_PRINCIPAL, String.format("%s=%s,%s", "cn", usrId, baseRdn));

			usrEnv.put(Context.SECURITY_CREDENTIALS, usrPw);

			new InitialLdapContext(usrEnv, null);	

       // 이 부분도 마찬가지로 ID, PW, 유저가 등록된 위치로 유저를 찾습니다.

		

 // 아래는 Active Directory에서 발생한 에러처리 입니다.

		}catch(AuthenticationException e){

			String msg = e.getMessage();

			

			  if (msg.indexOf("data 525") > 0) {             

					System.out.println("사용자를 찾을 수 없음.");

				} else if (msg.indexOf("data 773") > 0) { 

					System.out.println("사용자는 암호를 재설정해야합니다.");

				} else if (msg.indexOf("data 52e") > 0) {

					System.out.println("ID와 비밀번호가 일치하지 않습니다.확인 후 다시 시도해 주십시오.");

				} else if (msg.indexOf("data 533") > 0) {

					System.out.println("입력한 ID는 비활성화 상태 입니다.");

				} else if(msg.indexOf("data 532") > 0){

					System.out.println("암호가 만료되었습니다.");

				} else if(msg.indexOf("data 701") > 0){

					System.out.println("AD에서 계정이 만료됨");

				} else {

					System.out.println("정상!");

				}

			} 



     // 이 부분은 Active Directory와 JAVA가 연결 되지 않을 때의 예외처리입니다. 연결이 안되면 FAILED를 출력합니다.

		}catch(Exception nex){

			System.out.println("Active Directory Connection: FAILED");

			nex.printStackTrace();

		}

	}

}



(+ 추가



String searchFilter = String.format("(&(objectClass=person)(sAMAccountName=%s))", usrId);

			NamingEnumeration<SearchResult> results = ctx.search(baseRdn, searchFilter, ctls);





searchFilter를 "(&(objectClass=person)(sAMAccountName=%s))" 식으로 주었을 경우 person에 있는 sAMAccountName의 값으로 사용자를 검색할 수 있을 뿐더러 baseRdn을 굳이 ou나 o까지 안잡아도 "dc=admin, dc=com"(도메인)으로 설정해도 검색이 가능합니다.

(objectClass와 sAMAccountName의 구조를 아신다면 이해하기 쉽습니다...)



https://www.ibm.com/support/knowledgecenter/ko/SSL5ES_2.1.0/doc/iwd/ldap_setparam_mad.html

매개변수 및 필터 조건 참조 사이트입니다^^



+)



예를들어 "52e 잘못된 자격 증명" 에러처리는 자바에서 인증 오류가 발생했을 시의 에러코드가 



javax.naming.AuthenticationException:

[LDAP: error code 49 - 80090308: LdapErr: DSID-0C0903C5, 

comment: AcceptSecurityContext error, data 52e, v2580 ]  



이런식으로 출력되기 때문에 AuthenticationException 오류로 catch를 해서 마지막에 "data 52e"로 indexOf로 받아 온 예외처리입니다.





아래는 Active Directory의 인증에러 오류 코드입니다.



525	사용자를 찾을 수 없음

52e	잘못된 자격 증명

530	현재 로그온 할 수 없습니다.

531	이 워크 스테이션에서 로그온 할 수 없습니다.

532	암호가 만료되었습니다.

533 	계정 사용 안 함 

534	이 시스템에서 요청한 로그온 유형이 사용자에게 부여되지 않았습니다.

701	계정 만료

773	사용자는 암호를 재설정해야합니다.

775	사용자 계정 잠김
