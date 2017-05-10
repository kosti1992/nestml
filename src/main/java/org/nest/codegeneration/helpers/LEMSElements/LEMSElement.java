package org.nest.codegeneration.helpers.LEMSElements;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This abstract represents a super class for all lems elements, enabling the annotation and other operations.
 * @author  perun
 */
public abstract class LEMSElement {
    private Optional<String> mComment = Optional.empty();

    public Optional<String> getComment(){
        return this.mComment;
    }

    public void setComment(String _comment){
        if(_comment!=null){
            mComment = Optional.of(_comment);
        }
    }

    /**
     * Prints the comment as an array of strings, as required in the backend.
     * @return an array String objects which represent line by line the source code
     */
    @SuppressWarnings("unused")//used in the template
    public Object[] getCommentAsArray() {
        if(!mComment.isPresent()){
            return new String[0];
        }
        BufferedReader bufReader = new BufferedReader(new StringReader(mComment.get()));
        return bufReader.lines().toArray();
    }

}
