package projects.cbrenet.nodes.dataStructure;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  The difference between LinkedHashMap is that the method put would not move the
 *  entry to the tail. //may todo, though the hashmap insert the new entry to the last by default
 */

public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private final int initialCapacity;
    private final float loadFactor;
    private int maxCapacity = Integer.MAX_VALUE;

    public LRULinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true); // true to use lru
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
    }

    public void setMaxCapacity(int maxCapacity){
        this.maxCapacity = maxCapacity;
    }


    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size()>this.maxCapacity;
    }


    public Map.Entry<K, V> getHead(){
        try{
            return this.entrySet().iterator().next();
        }
        catch (NoSuchElementException noSuchElementException){
            return null;
        }
    }

    public boolean removeEldestEntry(){
        Map.Entry<K,V> entryTmp = this.getHead();
        if(entryTmp != null){
            this.remove(this.getHead().getKey());
            return true;
        }
        else{
            return false;
        }
    }

/*    public Map.Entry<K, V> getTail() throws NoSuchFieldException, IllegalAccessException{
        Field tail = super.getClass().getDeclaredField("tail");
        tail.setAccessible(true);
        return (Map.Entry<K, V>) tail.get(this);
    }*/


    public static void main(String[] args) {
        LRULinkedHashMap<Character, Integer> lru = new LRULinkedHashMap<Character, Integer>(
                16, 0.75f);
        String s = "ab";
        for (int i = 0; i < s.length(); i++) {
            lru.put(s.charAt(i), i);
        }
        System.out.println("LRU中key为h的Entry的值为： " + lru.get('h'));
        System.out.println("LRU的大小 ：" + lru.size());
        System.out.println("LRU ：" + lru);
        lru.put('z', 20);
        System.out.println("LRU ：" + lru);
        System.out.println(lru.entrySet());
        System.out.println(lru.removeEldestEntry());
        System.out.println("LRU ：" + lru);
        System.out.println(lru.removeEldestEntry());
        System.out.println("LRU ：" + lru);
        System.out.println(lru.removeEldestEntry());
        System.out.println("LRU ：" + lru);
        System.out.println(lru.removeEldestEntry());
        System.out.println("LRU ：" + lru);
    }
}
