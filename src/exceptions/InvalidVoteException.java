package exceptions;

public class InvalidVoteException extends Throwable {

    private String message;

    public InvalidVoteException(){
        message = "Le vote est invalide";
    }

    public InvalidVoteException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
