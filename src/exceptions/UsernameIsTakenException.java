package exceptions;

public class UsernameIsTakenException extends Throwable {

    private String message;

    public UsernameIsTakenException(){
        message = "Ce nom d'utilisateur n'est pas disponible";
    }

    public UsernameIsTakenException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
