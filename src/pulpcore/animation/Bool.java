/*
    Copyright (c) 2008, Interactive Pulp, LLC
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or without 
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright 
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright 
          notice, this list of conditions and the following disclaimer in the 
          documentation and/or other materials provided with the distribution.
        * Neither the name of Interactive Pulp, LLC nor the names of its 
          contributors may be used to endorse or promote products derived from 
          this software without specific prior written permission.
    
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
    POSSIBILITY OF SUCH DAMAGE.
*/

package pulpcore.animation;

/**
    An Bool is an boolean value that can be animated over time.
*/
public final class Bool extends Property {

    public Bool() {
        this(null, false);
    }
    
    public Bool(PropertyListener listener) {
        this(listener, false);
    }
    
    public Bool(boolean value) {
        this(null, value);
    }
    
    public Bool(PropertyListener listener, boolean value) {
        super(listener, value ? 1 : 0);
    }
    
    public boolean get() {
        return getValue() == 0 ? false : true;
    }
    
    protected void setValue(Number value) {
        setValue(value.intValue());
    }
    
    /**
        Sets the value of this Bool. 
        Any previous animations are stopped.
    */
    public void set(boolean value) {
        setValue(value?1:0);
        setBehavior(null);
    }
    
    /**
        Toggles the value of this Bool (same as the {@code ! } logical complement operator,
        inverting the value). 
        Any previous animations are stopped.
    */
    public void toggle() {
        set(!get());
    }
    
    /**
        Sets the value of this Bool after a specific delay. 
        Any previous animations are stopped.
    */
    public void set(boolean value, int delay) {
        setBehavior(new Tween(get()?1:0, value?1:0, delay, null, delay));
    }
    
    /**
        Binds this property to the specified property.
    */
    public void bindTo(Bool property) {
        setBehavior(new Binding(this, property));
    }
    
    /**
        Binds this property to the specified function.
    */
    public void bindTo(BindFunction function) {
        setBehavior(new Binding(this, function));
    }
    
    public String toString() {
        return get() ? "true" : "false";
    }
}