package ai.ivex.specdsl.generator

import org.eclipse.emf.ecore.resource.Resource


interface ArtifactGenerator {
	def String compile(Resource resource);
}