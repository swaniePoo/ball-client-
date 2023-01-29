package me.dinozoid.strife.property.implementations;

import me.dinozoid.strife.property.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MultiSelectEnumProperty<T extends Enum<T>> extends Property<List<T>> {

    public final T[] values;

    @SafeVarargs
    public MultiSelectEnumProperty(String label, Supplier<Boolean> dependency, T... values) {
        super(label, Arrays.asList(values), dependency);
        if (values.length == 0)
            throw new RuntimeException("Must have at least one default value.");
        this.values = constants();
    }

    @SafeVarargs
    public MultiSelectEnumProperty(String label, T... values) {
        this(label, () -> true, values);
    }

    public T[] constants() {
        return (T[]) value.get(0).getClass().getEnumConstants();
    }

    public T[] values() {
        return values;
    }

    public boolean selected(T variant) {
        return getValue().contains(variant);
    }

    public void value(int index, boolean selected) {
        final List<T> values = new ArrayList<>(value);
        final T referencedVariant = this.values[index];
        if (values.contains(referencedVariant)) {
            if (!selected)
                values.remove(referencedVariant);
        } else {
            if (selected)
                values.add(referencedVariant);
        }
        setValue(values);
    }
}