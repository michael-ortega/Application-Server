package appserver.job.impl;
//this class will actually compute the fibonacci result
public class FibonacciAux {
    //instantiate a number to store the final result
    Integer number = null;
    //set this number to be the number passed in to the class
    public FibonacciAux(Integer number) {
        this.number = number;
    }
    
    public Integer getResult() {
        return new Integer(fibonacci(number));
    }
    //a simple and efficient recursive function for getting any fibonacci number
    public int fibonacci(int n)  {
    if(n == 0)
        return 0;
    else if(n == 1)
        return 1;
    else
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
