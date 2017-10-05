## Pseudo code implementation algorithm

```
begin
    prepare tokenizer for parsing line of text
    
    while tokens exist on line
    
        get next token
    
        if not in compile mode
            if token is string literal
                push string onto data stack
                continue
            endif
            
            search dictionary for word
            
            if word found in dictionary
                execute it
            else
                try to parse word as number
                if numeric
                    push number onto data stack
                else
                    output error indication - word followed by ?
                    return false
                endif
            endif
            
        else ( in compile mode )
        
            if token is string literal
                add string to word being defined
                continue
            endif
        
            search dictionary for word
        
            if word found in dictionary
                if word marked as immediate
                    execute word
                else
                    add word to word being defined
                endif
            else ( word not found in dictionary )
                try to parse word as number
                if numeric
                    add number to word being defined
                else
                    output error indication - word followed by ?
                    return false
                endif
            endif
            
        endif
    end while
    
    return true
    
end

```