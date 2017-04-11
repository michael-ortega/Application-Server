package appserver.job.impl;

public class FibonacciAux {
    
    Integer number = null;
    
    public FibonacciAux(Integer number) {
        this.number = number;
    }
    
    public Integer getResult() {
        return new Integer(fibonacci(number));
    }

    public int fibonacci(int n)  {
    if(n == 0)
        return 0;
    else if(n == 1)
        return 1;
    else
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
