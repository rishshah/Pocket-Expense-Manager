import { MaterialIcons, } from '@expo/vector-icons';
import React from 'react';
import { View, TextInput, StyleSheet } from 'react-native';

import Colors from '../config/Colors';
import Size from '../config/Size';


function CustomInput({placeholder, secureTextEntry, color, iconName, value, setValue}) {
    return (
        <View style={styles.container}>
            <MaterialIcons style={styles.icon} name={iconName} size={24} color={color} />
            <TextInput 
                style={styles.text}
                value={value} 
                onChangeText={setValue}
                placeholder={placeholder} 
                secureTextEntry={secureTextEntry}/>
        </View>
    );
}

const styles = StyleSheet.create({
    container:{
        flexDirection: 'row',
        backgroundColor: Colors.white,
        borderColor: Colors.grey,
        borderWidth: 1,
        borderRadius: 5,
        paddingHorizontal: 10,
        paddingVertical: 2.5,
        marginVertical: 5,
        width: '80%',
        alignItems: 'center',

    },
    icon: {
        marginRight: 10,
        alignItems: 'center',
    },
    text: {
        borderLeftWidth: 1,
        fontSize: Size.font_m,
        borderLeftColor: Colors.grey,
        paddingLeft: 10,
    }
})

export default CustomInput;