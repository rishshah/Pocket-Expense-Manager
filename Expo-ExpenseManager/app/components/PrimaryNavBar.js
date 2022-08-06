import React, { useState } from 'react';
import { Alert, StyleSheet, Text, ToastAndroid, TouchableOpacity, View } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';

import Colors from '../config/Colors';
import Icons from '../config/Icons';
import Size from '../config/Size';
import { Menu, MenuDivider, MenuItem } from 'react-native-material-menu';
import { useNavigation } from '@react-navigation/native';


function CustomPrimaryNavBar() {
    const [visible, setVisible] = useState(false);
    const hideMenu = () => setVisible(false);
    const showMenu = () => setVisible(true);
    const navigation = useNavigation();

    const onLogoutPressed = () => {
        Alert.alert(
            'Confirm Logout',
            'Are you sure to logout ?',
            [
                {  
                    text: 'Cancel',  
                    onPress: () => {},
                    style: 'cancel',  
                },  
                {
                    text: 'Logout', 
                    onPress: async () => {
                        try {
                            await AsyncStorage.setItem('LoggedInUsername', '');
                            navigation.goBack();
                            ToastAndroid.show(`Logged Out !!`, ToastAndroid.SHORT);
                        } catch (error) {
                            console.warn('Error reading async storage');
                        }
                    },
                    style: 'ok',  
                }
            ],
        );
        hideMenu();
    }
    return (
        <View style={styles.navBar}>
            <Text style={styles.title}> Expenses </Text> 
            <Menu
                visible={visible}
                anchor={
                    <TouchableOpacity style={styles.settingsButton} onPress={showMenu}>
                        <MaterialIcons name={Icons.Menu} size={24} color={Colors.white} />
                    </TouchableOpacity>
                }
                onRequestClose={hideMenu}
            >
                <MenuItem onPress={hideMenu}>Statistics</MenuItem>
                <MenuItem onPress={hideMenu}>Balance</MenuItem>
                <MenuDivider color={Colors.tertiary}/>
                <MenuItem onPress={onLogoutPressed}>Logout</MenuItem>
            </Menu>
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
    settingsButton: {
        alignSelf: 'flex-end',
        padding: 10,
        top: 5,
    },
    title: {
        fontSize: Size.font_sl,
        fontWeight: '500',
        color: Colors.white,
        margin: 10,
    },
})
export default CustomPrimaryNavBar;