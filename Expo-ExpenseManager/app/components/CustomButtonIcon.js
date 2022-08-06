import { MaterialIcons } from '@expo/vector-icons';
import React from 'react';
import { Text, StyleSheet, Pressable } from 'react-native';


function CustomButtonIcon({title, iconName, onPress, bgColor, fgColor, style={}}) {
    return (
        <Pressable 
            onPress={onPress} 
            style={[
                styles.container, 
                bgColor && fgColor ? {backgroundColor: bgColor, borderColor: fgColor} : {},
                style 
            ]}>
            <MaterialIcons name={iconName} color={fgColor} size={24} />
            <Text 
                style={[
                    styles.text,
                    fgColor ? {color: fgColor} : {},
                ]} >
                    {title}
            </Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    container:{
        alignItems: 'center',
        borderWidth: 1,
        borderRadius: 5,
        paddingHorizontal: 5,
        paddingVertical: 10,
        marginTop: 10,
        flexDirection: 'row',
    },
    text: {
        fontWeight: 'bold',
        marginLeft: 5,
    },
})

export default CustomButtonIcon;