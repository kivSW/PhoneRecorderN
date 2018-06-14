package com.kivsw.phonerecorder.model.settings;

/**
 *  this class describes size of date in different units
 */

public class DataSize
{
    public enum Unit {BYTES, KBYTES, MBYTES, GBYTES, TBYTES};

    private long size;
    private Unit unit;
    public long getBytes()
    {
        long res=size;
        for(int i=unit.ordinal();i>0;i--)
            res *= 1024;
        return res;
    };
    public long getUnitSize(){return size;};
    public Unit getUnit(){return unit;}
    public DataSize(long size, int unit)
    {
        this(size, Unit.values()[unit]);
    };

    public DataSize(long size, Unit unit)
    {
        this.size=size;
        this.unit=unit;
    };
    @Override
    public boolean equals(Object o)
    {
        if(! (o instanceof DataSize) ) return false;

        return  (size == ((DataSize) o).size) &&
                (unit == ((DataSize) o).unit) ;
    }
}

