package my.learnings.osgi.rs;

import static my.learnings.osgi.rs.SecurityConstants.EXPIRATION_TIME;
import static my.learnings.osgi.rs.SecurityConstants.HEADER_STRING;
import static my.learnings.osgi.rs.SecurityConstants.SECRET;
import static my.learnings.osgi.rs.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import my.learnings.osgi.rs.User;

public class JWTAuthenticationFilter implements Filter {

	static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
	private static final String MATCH_ALL = "/login";
	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
	public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("JWTAuthenticationFilter - init");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException, JsonParseException {
		try {
			logger.info("doFilter - JWTAuthenticationFilter -----");
			HttpServletRequest tempRequest = (HttpServletRequest) request;
			HttpServletResponse tempResponse = (HttpServletResponse) response;
			logger.info("Request -" + tempRequest.getMethod() + " " + getRequestPath(tempRequest));
			if ("/".equals(getRequestPath(tempRequest)) || !requiresAuthentication(tempRequest, tempResponse)) {
				logger.info("requiresAuthentication - false");
				String token = tempRequest.getHeader("Authorization");
				if (token == "") {
					tempResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed:");
					logger.info("Authentication Failed:");
					return;
				}
				chain.doFilter(tempRequest, tempResponse);
				return;
			}
			InputStream is = tempRequest.getInputStream();
			int len = 0;
			byte[] buf = new byte[1024];
			StringBuffer sb = new StringBuffer();
			while ((len = is.read(buf)) != -1) {
				logger.info(new String(buf, 0, len));
				sb.append(new String(buf, 0, len));
			}
			logger.info("String from req:{}", sb.toString());
			User creds = new ObjectMapper().readValue(sb.toString(), User.class);
			logger.info("requiresAuthentication - {}", creds);
			if (creds == null || creds.getUsername() == null || creds.getUsername().isEmpty()
					|| creds.getPassword() == null || creds.getPassword().isEmpty()) {
				tempResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed:");
				logger.info("Authentication Failed:");
				return;
			}
			String token = Jwts.builder().setSubject(creds.getUsername())
					.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
					.signWith(SignatureAlgorithm.HS512, SECRET).compact();
			tempResponse.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
			tempResponse.getWriter().write("{\"" + TOKEN_PREFIX.trim() + "\":\"" + token + "\"}");
			tempResponse.getWriter().flush();
			tempResponse.getWriter().close();
			chain.doFilter(tempRequest, tempResponse);
		} catch (Exception e) {
			logger.error("Exception", e);
			return;
		}
		logger.info("JWTAuthenticationFilter - true");
		
	}

	@Override
	public void destroy() {
	}

	private boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String httpMethod = "POST";
		String pattern = request.getRequestURI().substring(request.getContextPath().length());
		if (pattern.equals(MATCH_ALL)) {
			logger.info("Request '" + getRequestPath(request) + "' matched by universal pattern '" + MATCH_ALL);
			return true;
		}
//		String path = getRequestPath(request);
//		logger.info("Checking match of request : '" + path + "'; against '" + pattern + "'");
//		if (path.startsWith(pattern)) {
//			logger.info("Request '" + request.getMethod() + " " + getRequestPath(request) + "'" + " match '" + " "
//					+ pattern);
//			return true;
//		}
		return false;
	}

	private String getRequestPath(HttpServletRequest request) {
		String url = request.getServletPath();
		String urlPath = request.getRequestURI().substring(request.getContextPath().length());
		if (request.getPathInfo() != null) {
			url += request.getPathInfo();
		}
		return url;
	}

}
