package mytown.new_protection.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.new_protection.Protection;
import mytown.new_protection.segment.*;
import mytown.new_protection.segment.enums.EntityType;
import mytown.new_protection.segment.enums.ItemType;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by AfterWind on 1/1/2015.
 * Serializes a protection
 */
public class ProtectionTypeAdapter extends TypeAdapter<Protection>{

    @Override
    public void write(JsonWriter out, Protection value) throws IOException {
        if(out == null) {
            return;
        }
        out.beginObject();
        out.name("modid").value(value.modid);
        out.name("segments").beginArray();
        for(Segment segment : value.segmentsTiles) {
            out.beginObject();
            out.name("class").value(segment.theClass.getName());
            out.name("type").value("tileEntity");
            out.name("condition").value(StringUtils.join(segment.conditionString, " "));
            out.name("flag").value(FlagType.modifyBlocks.toString());
            for (Map.Entry<String, List<Getter>> entry : segment.extraGettersMap.entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Getter getter : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(getter.element);
                    out.name("type").value(getter.type.toString());
                    out.endObject();
                }
                out.endArray();
            }
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Protection read(JsonReader in) throws IOException {

        String modid = null;
        String version = null;
        List<Segment> segments = new ArrayList<Segment>();

        String nextName;
        in.beginObject();
        while(!in.peek().equals(JsonToken.END_OBJECT)) {

            nextName = in.nextName();
            if (nextName.equals("modid")) {
                modid = in.nextString();
                if (modid == null)
                    throw new IOException("Missing modid for a protection!");
                if(!Loader.isModLoaded(modid) && !modid.equals("Vanilla")) {
                    MyTown.instance.log.info("   Skipped protection because the mod " + modid + " wasn't loaded.");
                    return null;
                } else if(version != null) {
                    if(!verifyVersion(modid, version)) {
                        MyTown.instance.log.info("   Skipped protection because it doesn't support the version loaded of mod: " + modid + " (" + version + ")");
                        return null;
                    }
                }
            } else if(nextName.equals("version")) {
                version = in.nextString();
                if(modid != null && !modid.equals("Vanilla")) {
                    if(!verifyVersion(modid, version)) {
                        MyTown.instance.log.info("   Skipped protection because it doesn't support the version loaded of mod: " + modid + " (" + version + ")");
                        return null;
                    }
                }
            } else if (nextName.equals("segments")) {
                in.beginArray();
                while (in.hasNext()) {

                    Segment segment = null;
                    String clazz = null, type = null;
                    EntityType entityType = null;
                    ItemType itemType = null;
                    String condition = null;
                    FlagType flag = null;
                    boolean isAdjacent = false;
                    int meta = -1;
                    Map<String, List<Getter>> extraGettersMap = new HashMap<String, List<Getter>>();

                    in.beginObject();
                    while (!in.peek().equals(JsonToken.END_OBJECT)) {
                        nextName = in.nextName();
                        if (nextName.equals("class")) {
                            clazz = in.nextString();
                            continue;
                        }
                        if (nextName.equals("type")) {
                            type = in.nextString();
                            if (type == null)
                                throw new IOException("The segment for class " + clazz + " does not have a type!");
                            continue;
                        }
                        if (nextName.equals("condition")) {
                            condition = in.nextString();
                            continue;
                        }
                        if(nextName.equals("flag")) {
                            flag = FlagType.valueOf(in.nextString());
                            continue;
                        }
                        // Checking if clazz and type is not null before anything else.
                        if (clazz == null)
                            throw new IOException("Class is not being specified in the protection with modid " + modid + ".");
                        if (type == null)
                            throw new IOException("Type is specified after the type-specific data for segment with class " + clazz + ".");


                        if (type.equals("entity")) {
                            if (nextName.equals("entityType")) {
                                entityType = EntityType.valueOf(in.nextString());
                                if (entityType == null)
                                    throw new IOException("Invalid entity type for segment with class " + clazz + ". Please choose hostile, passive or tracked.");
                                continue;
                            }
                        }
                        if (type.equals("item")) {
                            if (nextName.equals("itemType")) {
                                itemType = ItemType.valueOf(in.nextString());
                                if (itemType == null)
                                    throw new IOException("Invalid item type for segment with class " + clazz + ". Please choose breakBlock or use.");
                                continue;
                            }
                            if(nextName.equals("isAdjacent")) {
                                isAdjacent = in.nextBoolean();
                                continue;
                            }

                        }
                        if(type.equals("block")) {
                            if (nextName.equals("meta")) {
                                meta = in.nextInt();
                                continue;
                            }
                        }

                        // If it gets that means that it should be some extra data that will be used in checking something
                        extraGettersMap.put(nextName, parseGetters(in, clazz, nextName));
                    }

                    if (type != null) {
                        if (type.equals("tileEntity")) {
                            if(flag == null)
                                throw new IOException("The segment for class " + clazz + " does not have a valid flag!");
                            try {
                                segment = new SegmentTileEntity(Class.forName(clazz), extraGettersMap, flag, condition, IBlockModifier.Shape.rectangular);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " is invalid!");
                            }
                        } else if(type.equals("entity")){
                            if(entityType == null)
                                throw new IOException("EntityType is null for segment with class " + clazz);
                            try {
                                segment = new SegmentEntity(Class.forName(clazz), extraGettersMap, condition, entityType);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " is invalid!");
                            }
                        } else if(type.equals("item")) {
                            if(flag == null)
                                throw new IOException("The segment for class " + clazz + " does not have a valid flag!");
                            try {
                                segment = new SegmentItem(Class.forName(clazz), extraGettersMap, flag, condition, itemType, isAdjacent);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " is invalid!");
                            }
                        } else if(type.equals("block")) {
                            try {
                                segment = new SegmentBlock(Class.forName(clazz), extraGettersMap, condition, meta);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " in invalid!");
                            }
                        }
                    }

                    in.endObject();
                    if (segment == null)
                        throw new IOException("Segment with class " + clazz + " was not properly initialized!");
                    MyTown.instance.log.info("   Added segment for class: " + segment.theClass.getName());
                    segments.add(segment);
                }
                in.endArray();
            }
        }
        in.endObject();

        Protection protection;
        if(version == null)
            protection = new Protection(modid, segments);
        else
            protection = new Protection(modid, version, segments);
        return protection;
    }

    private List<Getter> parseGetters(JsonReader in, String clazz, String getterName) throws IOException {
        in.beginArray();
        List<Getter> getters = new ArrayList<Getter>();
        String nextName;
        while(in.hasNext()) {
            in.beginObject();
            String element = null;
            Getter.GetterType getterType = null;

            nextName = in.nextName();
            if(nextName.equals("element"))
                element = in.nextString();
            nextName = in.nextName();
            if(nextName.equals("type"))
                getterType = Getter.GetterType.valueOf(in.nextString());

            if(getterType == null)
                throw new IOException(getterName + " getter for segment with class " + clazz + " has an invalid getter type.");
            if(element == null)
                throw new IOException(getterName + " getter for segment with class " + clazz + " does not have a value.");
            in.endObject();
            getters.add(new Getter(element, getterType));
        }
        in.endArray();
        return getters;
    }

    private boolean checks(List<Getter>[] getters) {
        if(getters[0] == null || getters[1] == null || getters[2] == null || getters[3] == null) {
            return false;
        }
        return true;
    }

    private static boolean verifyVersion(String modid, String version) {
        for(ModContainer mod : Loader.instance().getModList()) {
            if(mod.getModId().equals(modid) && mod.getVersion().startsWith(version))
                return true;
        }
        return false;
    }

}
