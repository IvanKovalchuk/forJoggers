package com.kivsw.forjoggers.helper;

import java.util.HashSet;
import java.util.Set;

/**
 * This class counts how many other entities use a resource
 * UsingCounter class does not allow any entity use UsingCounter twice
 */
public class UsingCounter<T> {

    public interface IUsingChanged
    {
        void onUsingCountChanged(int usingCount);
    }

    private Set<T> userSet =new HashSet<T>(); // This set holds all the entities, that use a resource
    private IUsingChanged listener=null;

    public UsingCounter(IUsingChanged listener)
    {
        this.listener=listener;
    }
    private void doOnUsingCountChanged()
    {
        if(listener!=null) listener.onUsingCountChanged(userSet.size());
    }

    public boolean startUsingBy(T anInstance)
    {
        if(userSet.contains(anInstance))
            return false;//throw new Exception("UsingCounter has already contained "+anInstance.toString());
        userSet.add(anInstance);
        doOnUsingCountChanged();
        return true;
    }
    public boolean stopUsingBy(T anInstance)
    {
        if(!userSet.contains(anInstance))
            return false;//throw new Exception("UsingCounter has not contained "+anInstance.toString());
        userSet.remove(anInstance);
        doOnUsingCountChanged();
        return true;
    }

    public boolean contains(T anInstance )
    {
        return userSet.contains(anInstance);
    }
    public int count()
    {
        return userSet.size();
    }

    public Object[] array()
    {
       return  userSet.toArray();
    }
}
