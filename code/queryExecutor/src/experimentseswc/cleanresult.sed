s/<p>//g
s/<\/p>//g
s/&gt;/>/g
s/&lt;/</g
s/<a.*a>//
/Saved in parser cache/d

/<!--/ {
    :loop
        # pull in the next line to the pattern space
        N
        # if our line matches, delete entire pattern space
        # AND restart the cycle outside of the loop
        /-->/d
        # if we get here we didn't match delete, so keep looking
        b loop
}

1i\@prefix domOnto: <http://localhost/menuontology.owl/#>.