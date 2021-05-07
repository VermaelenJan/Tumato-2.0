# The Tumato language 

This guideline introduces the Tumato language its best practices for specifying behavior of autonomous systems.

The Tumato language contains the following blocks.

## State Vector

A state vector is a et of measurable discrete state variables that the behavior is concerned with. At runtime, each state variable is measured by a monitor. For example:

```
BEGIN STATE VECTOR
state S_flying can be flying, on_the_ground
state S_battery can be below_5, from_5_to_25, above_25
END STATE VECTOR
```
