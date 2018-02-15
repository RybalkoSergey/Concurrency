package bean;

/**
 * Created by SRYBALKO on 2/15/2018.
 */
public class Counter{

    private int count = 0;

    public int getCount() {
        return count;
    }

    public int increment(){
        return count = count + 1;
    }
}
