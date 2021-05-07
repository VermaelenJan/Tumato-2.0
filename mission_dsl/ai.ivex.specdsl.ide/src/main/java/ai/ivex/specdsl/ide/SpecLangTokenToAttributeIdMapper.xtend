package ai.ivex.specdsl.ide

import org.eclipse.xtext.ide.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper

public class SpecLangTokenToAttributeIdMapper extends AbstractAntlrTokenToAttributeIdMapper
{
	
	
	override protected calculateId(String tokenName, int tokenType) {
		println(tokenName)
    	if(tokenName.equals("BEGIN")) {
        	return 'begin'
        }
	}
}