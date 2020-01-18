package exceptions;

public class GameInProgressException extends Throwable {

    private String message;

    public GameInProgressException(){
        message = "Une partie est deja en cours";
    }

    public GameInProgressException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
