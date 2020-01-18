package exceptions;

public class HasAlreadyVotedException extends Throwable {

    private String message;

    public HasAlreadyVotedException(){
        message = "Has Already Voted Exception";
    }

    public HasAlreadyVotedException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
