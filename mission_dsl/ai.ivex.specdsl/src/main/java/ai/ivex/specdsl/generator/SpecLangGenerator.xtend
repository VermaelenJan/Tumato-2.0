/*
 * generated by Xtext 2.12.0
 */
package ai.ivex.specdsl.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class SpecLangGenerator extends AbstractGenerator {

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val jsonFileGenerator = new JsonFileGenerator()
		fsa.generateFile('spec.json', jsonFileGenerator.compile(resource))
		
		val protobufFileGenerator = new ProtobufFileGenerator()
		fsa.generateFile('spec.buf', protobufFileGenerator.compile(resource).toString)
	}
	
}

