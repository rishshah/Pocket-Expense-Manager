import AsyncStorage from '@react-native-async-storage/async-storage';
import { useIsFocused, useNavigation } from '@react-navigation/native';
import React, { useEffect, useState } from 'react';
import { Image, View, Text, StyleSheet, Alert, ToastAndroid } from 'react-native';
import CustomButton from '../components/CustomButton';
import CustomInput from '../components/CustomInput';

import { executeSql } from '../components/Sql';
import Colors from '../config/Colors';
import Icons from '../config/Icons';
import Size from '../config/Size';


function WelcomeScreen() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    
    const navigation = useNavigation();
    const navigateHome = () => {
        setTimeout(()=>{navigation.navigate('Home')}, 500);
    }

    const checkIfAlreadyLoggedIn = async () => {
        try {
            var value = await AsyncStorage.getItem('LoggedInUsername');
            if (value != null)
                navigateHome();
        } catch (error) {
            console.warn('Error reading async storage');
        }
    }
    
    const isFocused = useIsFocused();
    useEffect(() => {
        if (isFocused) {
            checkIfAlreadyLoggedIn();
        }
    }, [isFocused]);
    
    const onLoginPressed = () => {
        executeSql(
            `SELECT Name, Password FROM User WHERE Name = '${username}'`, 
            async (tx, results) => {
                var len = results.rows.length;
                if (len != 0) {
                    if (results.rows.item(0).Password == password) {
                        ToastAndroid.show(`${username} Logged In !!`, ToastAndroid.SHORT);
                        await AsyncStorage.setItem('LoggedInUsername', username);
                        navigateHome();
                    }
                    else {
                        Alert.alert('Warning!', 'Username/Password Incorrect!'); 
                    }
                } else {
                    Alert.alert('Warning!', `Username ${username} invalid!`); 
                }
            }
        );
    }

    const onCreateAccountPressed = () => {
        if (username.length == 0) {
            Alert.alert('Warning!', 'Name cannot be empty');
        }
        else {
            executeSql(
                `SELECT Name FROM User WHERE Name = '${username}'`, 
                (tx, results) => {
                    var len = results.rows.length;
                    if (len != 0) {
                        Alert.alert('Warning!', 'Username already exists!'); 
                    }
                    else {
                        executeSql(
                            `INSERT INTO User (Name, Password) VALUES('${username}', '${password}');`, 
                            async () => {
                                ToastAndroid.show(`${username}'s Account created!`, ToastAndroid.SHORT);
                                await AsyncStorage.setItem('LoggedInUsername', username);
                                navigateHome();
                            }
                        );
                    }
                }
            );
        }
    }

    return (
        <View style={styles.container}>
            <View style={styles.logoContainer}> 
                <Image style={styles.logoIcon} source={require('../assets/logo.png')}/>
                <Text style={styles.logoText}> Expense Manager </Text>
            </View>
            <CustomInput 
                placeholder='Username' value={username} setValue={setUsername} 
                color={Colors.secondary} iconName={Icons.Username}
            />
            <CustomInput 
                placeholder='Password' value={password} setValue={setPassword} secureTextEntry
                color={Colors.secondary} iconName={Icons.Password}
                />
            <CustomButton title='Login' onPress={onLoginPressed} bgColor={Colors.primary} fgColor={Colors.white}/>
            <CustomButton title='Create Account' onPress={onCreateAccountPressed} bgColor={Colors.secondary} fgColor={Colors.white}/>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Colors.background,
        alignItems: 'center',
    },
    logoContainer: {
        marginTop: 100,
        marginBottom: 20,
        alignItems: 'center',
    },
    logoIcon: {
        width: Size.logo,
        height: Size.logo,
        marginBottom: 10,
    },
    logoText: { 
        color: Colors.secondary, 
        fontSize: Size.font_sl,
        fontWeight: 'bold',
    },
});

  
export default WelcomeScreen;