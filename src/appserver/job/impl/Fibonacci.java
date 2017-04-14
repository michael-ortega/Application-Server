package appserver.job.impl;

import appserver.job.Tool;

public class Fibonacci implements Tool{
    //create a new instance of the helper class to calculate the fibonacci sequence
    FibonacciAux helper = null;
    
    @Override
    public Object go(Object parameters) {
        //call the helper class and pass in the parameters storing the result in the helper variable
        helper = new FibonacciAux((Integer) parameters);
	//then simply return the final result back
        return helper.getResult();
    }
}
