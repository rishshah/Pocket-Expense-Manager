import React from 'react';
import { Text, StyleSheet, Pressable } from 'react-native';
import Colors from '../config/Colors';

function CustomButton({title, onPress, bgColor, fgColor, type="primary", style={}}) {
    return (
        <Pressable 
            onPress={onPress} 
            style={[
                styles.container, 
                bgColor && fgColor ? {backgroundColor: bgColor, borderColor: fgColor} : {},
                style 
            ]}>
            <Text 
                style={[
                    styles.text,
                    styles[`text_${type}`],
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
        paddingHorizontal: 10,
        paddingVertical: 10,
        marginTop: 10,
        width: '80%',
    },
    container_primary: {
        backgroundColor: Colors.black,
        borderColor: Colors.black,
    },
    container_secondary: {
        borderColor: Colors.black,
    },
    text: {
        fontWeight: 'bold',
    },
    text_primary: {
        color: Colors.white,
    },
    text_secondary: {
        color: Colors.black,
    }
})

export default CustomButton;