package exceptions;

public class NotYourTurnException extends Throwable{

    private String message;

    public NotYourTurnException(){
        message = "Ce n'est pas à votre tour de jouer";
    }

    public NotYourTurnException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
