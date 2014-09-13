package mytown.entities;

// TODO Implement PlotType

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.interfaces.IHasBlockWhitelists;
import mytown.entities.interfaces.IHasFlags;
import mytown.entities.interfaces.IHasResidents;
import mytown.proxies.DatasourceProxy;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Joe Goett
 */
public class Plot implements IHasFlags, IHasResidents {
    private int db_ID;
    private int dim, x1, y1, z1, x2, y2, z2;
    private Town town;
    private String key, name;

    public static int minX = 5;
    public static int minY = 4;
    public static int minZ = 5;

    public Plot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (x1 > x2) {
            int aux = x2;
            x2 = x1;
            x1 = aux;
        }

        if (z1 > z2) {
            int aux = z2;
            z2 = z1;
            z1 = aux;
        }

        if (y1 > y2) {
            int aux = y2;
            y2 = y1;
            y1 = aux;
        }
        // Second parameter is always highest
        this.name = name;
        this.town = town;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.dim = dim;


        updateKey();
    }

    public int getDim() {
        return dim;
    }

    public int getStartX() {
        return x1;
    }

    public int getStartY() {
        return y1;
    }

    public int getStartZ() {
        return z1;
    }

    public String getStartCoordString() {
        return String.format("%s, %s, %s", x1, y1, z1);
    }

    public int getEndX() {
        return x2;
    }

    public int getEndY() {
        return y2;
    }

    public int getEndZ() {
        return z2;
    }

    public String getEndCoordString() {
        return String.format("%s, %s, %s", x2, y2, z2);
    }

    public int getStartChunkX() {
        return x1 >> 4;
    }

    public int getStartChunkZ() {
        return z1 >> 4;
    }

    public int getEndChunkX() {
        return x2 >> 4;
    }

    public int getEndChunkZ() {
        return z2 >> 4;
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    private void updateKey() {
        key = String.format("%s;%s;%s;%s;%s;%s;%s", dim, x1, y1, z1, x2, y2, z2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDb_ID(int ID) {this.db_ID = ID; }

    public int getDb_ID() {return this.db_ID; }

    /**
     * Checks if the coords are within this plot and in the same dimension
     *
     * @param dim
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isCoordWithin(int dim, int x, int y, int z) { // TODO Is dim really needed?
        return dim == this.dim && x1 <= x && x <= x2 && y1 <= y && y <= y2 && z1 <= z && z <= z2;
    }

    @Override
    public String toString() {
        return String.format("Plot: {Name: %s, Dim: %s, Start: [%s, %s, %s], End: [%s, %s, %s]}", name, dim, x1, y1, z1, x2, y2, z2);
    }

    /* ---- IHasFlags ----- */

    private List<Flag> flags = new ArrayList<Flag>();

    @Override
    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    @Override
    public boolean hasFlag(String name) {
        for(Flag flag : flags)
            if(flag.getName().equals(name))
                return true;
        return false;
    }

    @Override
    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flags);
    }

    @Override
    public Flag getFlag(String name) {
        for(Flag flag : flags)
            if(flag.getName().equals(name))
                return flag;
        return null;
    }

    /* ---- IHasResidents ----- */

    private List<Resident> whitelist = new ArrayList<Resident>();

    @Override
    public void addResident(Resident res) {
        whitelist.add(res);
    }

    @Override
    public void removeResident(Resident res) {
        whitelist.remove(res);
    }

    @Override
    public boolean hasResident(Resident res) {
        return whitelist.contains(res);
    }

    @Override
    public ImmutableList<Resident> getResidents() {
        return ImmutableList.copyOf(whitelist);
    }

    private List<Resident> owners = new ArrayList<Resident>();

    public void addOwner(Resident res) {
        owners.add(res);
    }

    public void removeOwner(Resident res) {
        owners.remove(res);
    }

    public boolean hasOwner(Resident res) {
        return owners.contains(res);
    }

    public ImmutableList<Resident> getOwners() {
        return ImmutableList.copyOf(owners);
    }
}
