import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import Colors from '../config/Colors';
import Size from '../config/Size';

function StatisticsScreen() {
    return (
        <View style={styles.container}>
            <Text style={styles.title}> Statistics and Charts </Text>
        </View>
    );
}
const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingTop: 20,
        backgroundColor: Colors.background,
    },
    title: {
        fontSize: Size.font_sl,
        fontWeight: '500',
        color: Colors.secondary,
        marginTop: 60,
        marginBottom: 30,
    },
})
export default StatisticsScreen;