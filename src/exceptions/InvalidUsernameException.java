package exceptions;

public class InvalidUsernameException extends Throwable {

    private String message;

    public InvalidUsernameException(){
        message = "Nom d'utilisateur invalide";
    }

    public InvalidUsernameException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
