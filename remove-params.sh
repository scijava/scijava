#perl -0777 -i -pe 's/(\@plugin[^\n]*\n)\@Parameter.key = "([^"]*)".\n/\1\/**\n * TODO\n *\n * \@param \2\n/igs' **/*.java
#perl -0777 -i -pe 's/\@Parameter.key = "([^"]*)"[^\n]*.\n/ * \@param \1\n/igs' **/*.java
#perl -0777 -i -pe 's/( \* \@param[^\n]*\n)([^ ])/\1 *\/\n\2/igs' **/*.java
#perl -0777 -i -pe 's/ \*\/\n([\s]*[\*])/\1/igs' **/*.java
#perl -0777 -i -pe 's/ \*\/\n([\s]*\@Parameter)/\1/igs' **/*.java
#perl -0777 -i -pe 's/ \*\/\n([\s]*\/\/[\s]*\*)/\1/igs' **/*.java
perl -0777 -i -pe 's/ \* \@param output([^\n]*\n[\s]*\*\/\n[\s]*public[^\(]*apply\()/ \* \@return the output\1/igs' **/*.java

