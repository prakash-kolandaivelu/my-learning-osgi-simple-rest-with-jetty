package my.learnings.osgi.rs;

import static my.learnings.osgi.rs.SecurityConstants.HEADER_STRING;
import static my.learnings.osgi.rs.SecurityConstants.SECRET;
import static my.learnings.osgi.rs.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.util.List;
import org.json.*;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import my.learnings.osgi.rs.HttpRequestWrapper;

public class JWTAuthorizationFilter implements Filter {

	static final Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("doFilter - JWTAuthorizationFilter -----init");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.info("doFilter - JWTAuthorizationFilter -----");
		HttpServletRequest tempRequest = (HttpServletRequest) request;
		HttpServletResponse tempResponse = (HttpServletResponse) response;
		logger.info("Request -" + tempRequest.getMethod() + " " + getRequestPath(tempRequest));
		String defaultToken = tempResponse.getHeader(HEADER_STRING);
		String userToken = tempRequest.getHeader("Authorization");
		String httpMethod = tempRequest.getMethod();
		String httpPath = getRequestPath(tempRequest);
		logger.info("Request -defaultToken={}", defaultToken);
		if (defaultToken == null && httpPath.equals("/")) {
			logger.info("Skipping the authorization process... -----");
			chain.doFilter(tempRequest, response);
			return;
		} else if (userToken != null) {
			JSONObject jo = new JSONObject();
			try {
				jo = new JSONObject(userToken);
			} catch (Exception e) {
				logger.error("EXCETIPN: ", e);
				return;
			}
			defaultToken = "" + TOKEN_PREFIX + " " + jo.get("Bearer");
		} else {
			if (defaultToken == null || !defaultToken.startsWith(TOKEN_PREFIX)) {
				tempResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Failed");
				return;
			}
		}
		String user = null;
		List<String> roles = null;
		if (defaultToken != null) {
			// parse the token.
			user = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(defaultToken.replace(TOKEN_PREFIX, "")).getBody()
					.getSubject();
		}
		logger.info("doFilter - JWTAuthorizationFilter --user:{}", user);
		if (user == null) {
			tempResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Failed:");
			logger.info("Authorization Failed:");
		}
		chain.doFilter(tempRequest, response);
	}

	@Override
	public void destroy() {
	}

	private String getRequestPath(HttpServletRequest request) {
		String url = request.getServletPath();
		String urlpath = request.getRequestURI().substring(request.getContextPath().length());
		logger.info(urlpath);
		if (request.getPathInfo() != null) {
			url += request.getPathInfo();
		}
		return url;
	}
}
