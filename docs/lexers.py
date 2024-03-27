from pygments.lexer import inherit
from pygments.lexers.jvm import GroovyLexer
from pygments.token import Comment

class SciJavaGroovyLexer(GroovyLexer):
    tokens = {
        'base': [
            (r'#@.*?$', Comment.Single),
            inherit,
        ]
    }