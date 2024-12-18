/*
 * generated by Xtext 2.12.0
 */
package ai.ivex.specdsl.web

import javax.servlet.annotation.WebServlet
import org.eclipse.xtext.util.DisposableRegistry
import org.eclipse.xtext.web.servlet.XtextServlet

/**
 * Deploy this class into a servlet container to enable DSL-specific services.
 */
@WebServlet(name = 'XtextServices', urlPatterns = '/xtext-service/*')
class SpecLangServlet extends XtextServlet {
	
	DisposableRegistry disposableRegistry
	
	override init() {
		super.init()
		val injector = new SpecLangWebSetup().createInjectorAndDoEMFRegistration()
		disposableRegistry = injector.getInstance(DisposableRegistry)
	}
	
	override destroy() {
		if (disposableRegistry !== null) {
			disposableRegistry.dispose()
			disposableRegistry = null
		}
		super.destroy()
	}
	
//	override protected doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			resp.addHeader('Access-Control-Allow-Origin', '*')
//			resp.addHeader('Access-Control-Allow-Methods', 'GET, HEAD, POST, PUT, TRACE, OPTIONS')
//			resp.addHeader('Access-Control-Allow-Credentials', 'true')
//			super.doOptions(req, resp);
//	}
//	override protected doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			resp.addHeader('Access-Control-Allow-Origin', '*')
//			resp.addHeader('Access-Control-Allow-Methods', 'GET, HEAD, POST, PUT, TRACE, OPTIONS')
//			resp.addHeader('Access-Control-Allow-Credentials', 'true')
//			super.doHead(req, resp);
//	} 
//	override protected doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			resp.addHeader('Access-Control-Allow-Origin', '*')
//			resp.addHeader('Access-Control-Allow-Methods', 'GET, HEAD, POST, PUT, TRACE, OPTIONS')
//			resp.addHeader('Access-Control-Allow-Credentials', 'true')
//			super.doGet(req, resp);
//	} 	
//	override protected doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			resp.addHeader('Access-Control-Allow-Origin', '*')
//			resp.addHeader('Access-Control-Allow-Methods', 'GET, HEAD, POST, PUT, TRACE, OPTIONS')
//			resp.addHeader('Access-Control-Allow-Credentials', 'true')
//			super.doPost(req, resp);
//	}
//	override protected doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			resp.addHeader('Access-Control-Allow-Origin', '*')
//			resp.addHeader('Access-Control-Allow-Methods', 'GET, HEAD, POST, PUT, TRACE, OPTIONS')
//			resp.addHeader('Access-Control-Allow-Credentials', 'true')
//			super.doPut(req, resp);
//	} 
//	override protected doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			resp.addHeader('Access-Control-Allow-Origin', '*')
//			resp.addHeader('Access-Control-Allow-Methods', 'GET, HEAD, POST, PUT, TRACE, OPTIONS')
//			resp.addHeader('Access-Control-Allow-Credentials', 'true')
//			super.doTrace(req, resp);
//	}  
	
}
