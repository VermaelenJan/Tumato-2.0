/*
 * generated by Xtext 2.12.0
 */
package ai.ivex.specdsl


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class SpecLangStandaloneSetup extends SpecLangStandaloneSetupGenerated {

	def static void doSetup() {
		new SpecLangStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}
