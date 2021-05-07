package ai.ivex.specdsl.ide

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultSemanticHighlightingCalculator
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.impl.CrossReferenceImpl
import org.eclipse.xtext.impl.KeywordImpl
import org.eclipse.xtext.impl.ParserRuleImpl
import org.eclipse.xtext.impl.RuleCallImpl
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.util.CancelIndicator
import ai.ivex.specdsl.specLang.Model
import ai.ivex.specdsl.specLang.NestedGoalList
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.impl.ActionImpl

class SpecLangSemanticHighlightingCalculator extends DefaultSemanticHighlightingCalculator {

	
	override protected highlightElement(EObject object, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		var node = NodeModelUtils.getNode(object)
		acceptor.addPosition(node.getOffset(), node.getLength(), object.eClass.name)
		object.addGoalListDepthData(acceptor)
		for(child : node.children){
			val el = child.grammarElement
			switch(el){
				CrossReferenceImpl: 
					{
						val target = el.type.classifier;
						acceptor.addPosition(child.getOffset(), child.getLength(), target.name + "-reference", "reference")
					}
				KeywordImpl:
					{
						acceptor.addPosition(child.getOffset(), child.getLength(), "keyword", "keyword-" + el.value)		
					}
				RuleCallImpl:
					{
						if (el.rule instanceof ParserRuleImpl){
							acceptor.addPosition(child.getOffset(), child.getLength(), el.rule.name)
						}
					}				
			}
		}
		false
	}
	
	def addGoalListDepthData(EObject object, IHighlightedPositionAcceptor acceptor){
		var node = NodeModelUtils.getNode(object)
		var depth = calculateGoalListDepth(node)
		if (depth > 0){
			acceptor.addPosition(node.getOffset(), node.getLength(), "NestedGoalList-depth-" + depth.toString)	
		}
	}
	
	def int calculateGoalListDepth(ICompositeNode node){
		if (node === null) return 0
		
		if (!(node.grammarElement instanceof ActionImpl)) return node.parent.calculateGoalListDepth		
		
		var object = NodeModelUtils.findActualSemanticObjectFor(node)
		
		switch(object){
			Model: 0
			NestedGoalList: 1 + node.parent.calculateGoalListDepth
			default: node.parent.calculateGoalListDepth
		}
		
	}
}
