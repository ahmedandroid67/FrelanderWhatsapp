import { View, Text, StyleSheet, Pressable, Platform, ScrollView, Linking } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { changeLanguage } from '@/lib/i18n';
import { useColors } from '@/hooks/useColors';
export default function SettingsScreen() {
  const { t, i18n } = useTranslation();
  const colors = useColors();

  const languages = [
    { code: 'en', label: t('english') },
    { code: 'fr', label: t('french') },
    { code: 'ar', label: t('arabic') },
    { code: 'es', label: t('spanish') },
  ];

  const handleLanguageChange = async (langCode: string) => {
    if (i18n.language === langCode) return;
    
    await changeLanguage(langCode);
    
    // In React Native, changing RTL status often requires an app reload to take effect
    // on layout. We can use expo-updates if we want a full reload, or trust that our
    // React state handles text direction. Native view mirroring needs a restart though.
    // For now, we update state immediately. 
  };

  return (
    <ScrollView 
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={{ padding: 16, paddingBottom: 40 }}
    >
      <Text style={[styles.title, { color: colors.foreground }]}>{t('language')}</Text>
      
      <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
        {languages.map((lang, index) => (
          <Pressable
            key={lang.code}
            style={[
              styles.row,
              index !== languages.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.border }
            ]}
            onPress={() => handleLanguageChange(lang.code)}
          >
            <Text style={[
              styles.langText, 
              { color: colors.foreground },
              i18n.language === lang.code && { color: colors.primary, fontWeight: 'bold' }
            ]}>
              {lang.label}
            </Text>
            {i18n.language === lang.code && (
              <Text style={{ color: colors.primary }}>✓</Text>
            )}
          </Pressable>
        ))}
      </View>

      <Text style={[styles.title, { color: colors.foreground, marginTop: 24 }]}>{t('messageTemplates')}</Text>
      
      <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Pressable 
          style={styles.row}
          onPress={() => router.push('/templates')}
        >
          <View style={styles.left}>
            <View style={[styles.iconCircle, { backgroundColor: colors.primary + '18' }]}>
              <Feather name="message-square" size={18} color={colors.primary} />
            </View>
            <View>
              <Text style={[styles.label, { color: colors.foreground }]}>{t('messageTemplates')}</Text>
              <Text style={[styles.value, { color: colors.mutedForeground }]}>{t('variableGuide')}</Text>
            </View>
          </View>
          <Feather name="chevron-right" size={16} color={colors.mutedForeground} />
        </Pressable>
      </View>

      <Text style={[styles.title, { color: colors.foreground, marginTop: 24 }]}>{t('aboutApp')}</Text>
      
      <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <View style={styles.row}>
          <View style={styles.left}>
            <View style={[styles.iconCircle, { backgroundColor: colors.primary + '18' }]}>
              <Feather name="user" size={18} color={colors.primary} />
            </View>
            <View>
              <Text style={[styles.label, { color: colors.foreground }]}>{t('creator')}</Text>
              <Text style={[styles.value, { color: colors.mutedForeground }]}>k.ahmed.lara</Text>
            </View>
          </View>
        </View>

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <Pressable 
          style={styles.row}
          onPress={() => Linking.openURL('https://laaraichi.com')}
        >
          <View style={styles.left}>
            <View style={[styles.iconCircle, { backgroundColor: '#8B5CF618' }]}>
              <Feather name="globe" size={18} color="#8B5CF6" />
            </View>
            <View>
              <Text style={[styles.label, { color: colors.foreground }]}>{t('website')}</Text>
              <Text style={[styles.value, { color: colors.mutedForeground }]}>laaraichi.com</Text>
            </View>
          </View>
          <Feather name="external-link" size={14} color={colors.mutedForeground} />
        </Pressable>

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <Pressable 
          style={styles.row}
          onPress={() => {
            const phone = '+212666289222';
            const url = `whatsapp://send?phone=${phone.replace(/\D/g, '')}`;
            Linking.canOpenURL(url).then(supported => {
              if (supported) Linking.openURL(url);
              else Linking.openURL(`https://wa.me/${phone.replace(/\D/g, '')}`);
            });
          }}
        >
          <View style={styles.left}>
            <View style={[styles.iconCircle, { backgroundColor: '#25D36618' }]}>
              <Feather name="message-circle" size={18} color="#25D366" />
            </View>
            <View>
              <Text style={[styles.label, { color: colors.foreground }]}>{t('whatsapp')}</Text>
              <Text style={[styles.value, { color: colors.mutedForeground }]}>+212 666 289 222</Text>
            </View>
          </View>
          <Feather name="external-link" size={14} color={colors.mutedForeground} />
        </Pressable>

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <View style={styles.row}>
          <View style={styles.left}>
            <View style={[styles.iconCircle, { backgroundColor: colors.muted }]}>
              <Feather name="info" size={18} color={colors.mutedForeground} />
            </View>
            <View>
              <Text style={[styles.label, { color: colors.foreground }]}>{t('version')}</Text>
              <Text style={[styles.value, { color: colors.mutedForeground }]}>1.0.0 (Build 1)</Text>
            </View>
          </View>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  title: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 16,
    fontFamily: 'Inter_600SemiBold',
  },
  card: {
    borderRadius: 12,
    borderWidth: 1,
    overflow: 'hidden',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
  },
  langText: {
    fontSize: 16,
    fontFamily: 'Inter_500Medium',
  },
  left: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  iconCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
  },
  value: {
    fontSize: 12,
    marginTop: 1,
  },
  divider: {
    height: 1,
    marginLeft: 64,
  },
});
