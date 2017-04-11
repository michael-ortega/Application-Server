package appserver.job.impl;

import appserver.job.Tool;

public class Fibonacci implements Tool{

    FibonacciAux helper = null;
    
    @Override
    public Object go(Object parameters) {
        
        helper = new FibonacciAux((Integer) parameters);
        return helper.getResult();
    }
}
