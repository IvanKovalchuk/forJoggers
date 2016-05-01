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
    public void startUsingBy(T anInstance)
    {
        if(userSet.contains(anInstance))
            return;//throw new Exception("UsingCounter has already contained "+anInstance.toString());
        userSet.add(anInstance);
        doOnUsingCountChanged();
    }
    public void stopUsingBy(T anInstance)
    {
        if(!userSet.contains(anInstance))
            return;//throw new Exception("UsingCounter has not contained "+anInstance.toString());
        userSet.remove(anInstance);
        doOnUsingCountChanged();
    }
}
