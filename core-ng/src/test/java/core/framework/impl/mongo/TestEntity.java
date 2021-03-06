package core.framework.impl.mongo;

import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
@Collection(name = "test_entity")
public class TestEntity {
    public static final String INT_FIELD = "int_field";

    @Id
    public ObjectId id;

    @Field(name = INT_FIELD)
    public Integer intField;

    @Field(name = "double_field")
    public Double doubleField;

    @Field(name = "date_field")
    public LocalDateTime dateField;

    @Field(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    @Field(name = "string_field")
    public String stringField;

    @Field(name = "list_field")
    public List<String> listField;

    @Field(name = "map_field")
    public Map<String, String> mapField;

    @Field(name = "child")
    public TestEntityChild child;

    @Field(name = "children")
    public List<TestEntityChild> children;

    @Field(name = "children_map")
    public Map<String, TestEntityChild> childrenMap;

    @Field(name = "null_child")
    public TestEntityChild nullChild;
}
