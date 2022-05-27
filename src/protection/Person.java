package protection;

abstract public class Person {
    private final String name;
    private final String pwd;

    public Person(String name, String pwd){
         this.name = name;
         this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public String getPwd() {
        return pwd;
    }
}
