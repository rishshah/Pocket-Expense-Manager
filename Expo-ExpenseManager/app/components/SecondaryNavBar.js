import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

import Colors from '../config/Colors';
import Size from '../config/Size';


function SecondaryNavBar({title}) {
    return (
        <View style={styles.navBar}>
            <Text style={styles.title}> {title} </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    navBar: {
        flexDirection: 'row',
        backgroundColor: Colors.primary,
        paddingRight: 10,
        justifyContent: 'space-between',
    },
    title: {
        fontSize: Size.font_sl,
        fontWeight: '500',
        color: Colors.white,
        margin: 10,
    },
})
export default SecondaryNavBar;