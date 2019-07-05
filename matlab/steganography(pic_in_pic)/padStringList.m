%% 
% .
% Add Padding to a String List
% The function takes a string vector input, a character input (used for padding), 
% a limit integer value (the total number of characters in the string), and 
% an integer that indicates the padding location (0 = Left; 1 = Right).
%The function resturns the padded string vector.
function newList = padStringList( list, character, limit, direction )
    [rSize, cSize] = size(list); %Get number of rows and cols in list

    if cSize < limit
        tempStrList = '';
        for x = 1:rSize
            paddedStr = padString(list(x, :), character, limit, direction);
            tempStrList(x, :) = paddedStr;
        end
        newList = tempStrList;
    else
        newList = list;
    end
end

