#/bin/bash

# Helper function to convert inner classes from package.Cls.ICls to package.Cls$ICls
format_inner_class () {
  if [[ `expr match "$1" '.*\.[A-Z].*\.[A-Z].*'` > 0 ]];
  then
    pre=`expr match "$1" '\(.*\.[A-Z].*\)\.[A-Z].*'`
    suffix=`expr match "$1" '.*\.[A-Z].*\.\([A-Z].*\)'`
    echo "$pre\$$suffix"
  else
    echo "$1"
  fi
}

# Ensure running in basedir
dir=${PWD##*/}          # Credit: https://stackoverflow.com/a/1371283
if [[ "$dir" != "scijava" ]]
then
  echo "Please run this script from the scijava base directory."
  exit 0
fi

# Translate module-info.java to META-INF entries
for module in $(find . -name "module-info.java")
do
  if [[ "$DEBUG" == true ]]
  then
    echo "processing $module"
  fi
  basedir="${module%src/main/java/module-info\.java}src/main/resources/META-INF/services/"

  # Clear old entries
  rm -rf "$basedir"

  CURRENT_FILE=
  FOUND_SERVICE=false
  # Read file line-by-line
  while IFS= read -r line
  do
    line_arr=($line)
    if [[ "$FOUND_SERVICE" == true ]]
    then
      service="${line_arr[0]}"
      # Strip trailing ,/;
      service="${service%%,*}"
      service="${service%%;*}"
      # Strip whitespace
      service="${service#[[:space:]]*}"

      # skip the line if empty
      if [[ -z "$service" ]]
      then
        continue
      fi

      service=$(format_inner_class "$service")

      # Append the line to the current file
      echo "$service" >> "$CURRENT_FILE"
      if [[ "$DEBUG" == true ]]
      then
        echo "found service impl: $service"
      fi

      # Check if this is the end of the block
      if [[ "$line" == *";"* ]]
      then
        FOUND_SERVICE=false
      fi
    elif [[ "$line" == *"provides"* ]] 
    then
      # If we find a "provides <class> with" line, create a new file with value "class" in META-INF/services/

      # Make the META-INF folder if it doesn't exist already
      mkdir -p "$basedir"

      # Extract the class name and create the base file
      CURRENT_FILE="$basedir${line_arr[1]}"
      if [[ "$DEBUG" == true ]]
      then
        echo "found service provider: Creating $CURRENT_FILE"
      fi
      echo "# -- DO NOT EDIT -- This file is autogenerated" >> "$CURRENT_FILE"
      echo "# Instead, modify and re-run <basedir>/bin/generate-meta-inf.sh from the top-level" >> "$CURRENT_FILE"

      if [[ "$line" == *";"* ]]
      then
        # if this line ends with a semi-colon then it includes a single service 
        service="${line_arr[3]}"
        service="${service%%;*}"
        service=$(format_inner_class "$service")
        echo "$service" >> "$CURRENT_FILE"
        if [[ "$DEBUG" == true ]]
        then
          echo "found single service impl: $service"
        fi
      else
        # Process service implementations line-by-line
        FOUND_SERVICE=true
      fi
    fi
  done < "$module"
  # Add comment that file is autogenerated and to re-run this method
  # For each following line:
  #   if the line ends with semi-colon we know we're at the end
  #   else add the current line (without indent and without last character) to the current file
done
